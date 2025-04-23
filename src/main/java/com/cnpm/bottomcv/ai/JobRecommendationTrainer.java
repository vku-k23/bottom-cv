package com.cnpm.bottomcv.ai;

import com.cnpm.bottomcv.model.Apply;
import com.cnpm.bottomcv.model.CV;
import com.cnpm.bottomcv.model.Job;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.ApplyRepository;
import com.cnpm.bottomcv.repository.CVRepository;
import com.cnpm.bottomcv.repository.JobRepository;
import com.cnpm.bottomcv.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class JobRecommendationTrainer {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final CVRepository cvRepository;
    private final ApplyRepository applyRepository;
    private final TFIDFVectorizer tfidfVectorizer;

    public void trainModel() throws Exception {
        // 1. Chuẩn bị dữ liệu
        List<User> users = userRepository.findAll();
        List<Job> jobs = jobRepository.findAll();
        List<Apply> applies = applyRepository.findAll();

        // 2. Xây dựng chỉ mục TF-IDF
        tfidfVectorizer.buildIndex(users, jobs);
        int featureSize = tfidfVectorizer.getFeatureSize(); // Kích thước vector đặc trưng thực tế
        int totalFeatureSize = featureSize * 2; // Kết hợp đặc trưng của user và job

        List<DataSet> dataSets = new ArrayList<>();
        Random random = new Random();

        // 3. Tạo dữ liệu huấn luyện
        for (Apply apply : applies) {
            User user = userRepository.findById(apply.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Job job = jobRepository.findById(apply.getJob().getId())
                    .orElseThrow(() -> new RuntimeException("Job not found"));

            // Trích xuất đặc trưng bằng TF-IDF
            double[] userFeatures = extractUserFeatures(user);
            double[] jobFeatures = extractJobFeatures(job);

            // Kết hợp đặc trưng
            double[] combinedFeatures = new double[totalFeatureSize];
            System.arraycopy(userFeatures, 0, combinedFeatures, 0, featureSize);
            System.arraycopy(jobFeatures, 0, combinedFeatures, featureSize, featureSize);

            // Tạo DataSet (positive sample)
            INDArray features = Nd4j.create(combinedFeatures);
            INDArray labels = Nd4j.create(new double[]{1.0}); // Nhãn: 1 (đã ứng tuyển)
            dataSets.add(new DataSet(features, labels));
        }

        // Tạo negative samples (các cặp user-job không có tương tác)
        int negativeSampleSize = applies.size();
        for (int i = 0; i < negativeSampleSize; i++) {
            User user = users.get(random.nextInt(users.size()));
            Job job = jobs.get(random.nextInt(jobs.size()));

            // Kiểm tra xem cặp này có trong applies không
            if (applies.stream().noneMatch(a -> a.getUser().getId().equals(user.getId()) && a.getJob().getId().equals(job.getId()))) {
                double[] userFeatures = extractUserFeatures(user);
                double[] jobFeatures = extractJobFeatures(job);

                double[] combinedFeatures = new double[totalFeatureSize];
                System.arraycopy(userFeatures, 0, combinedFeatures, 0, featureSize);
                System.arraycopy(jobFeatures, 0, combinedFeatures, featureSize, featureSize);

                INDArray features = Nd4j.create(combinedFeatures);
                INDArray labels = Nd4j.create(new double[]{0.0}); // Nhãn: 0 (không ứng tuyển)
                dataSets.add(new DataSet(features, labels));
            }
        }

        // 4. Xây dựng mô hình Neural Network
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.0001)) // Learning rate thực tế, điều chỉnh nếu cần
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(totalFeatureSize)
                        .nOut(256) // Tăng số nơ-ron để xử lý vector lớn
                        .activation(Activation.RELU)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(256)
                        .nOut(128)
                        .activation(Activation.RELU)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.XENT)
                        .nIn(128)
                        .nOut(1)
                        .activation(Activation.SIGMOID)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));

        // 5. Huấn luyện mô hình
        DataSetIterator iterator = new ListDataSetIterator<>(dataSets, 64); // Batch size thực tế
        for (int i = 0; i < 50; i++) { // Số epoch thực tế, có thể điều chỉnh
            iterator.reset();
            model.fit(iterator);
        }

        // 6. Lưu mô hình
        File modelFile = new File("recommendation-model.zip");
        ModelSerializer.writeModel(model, modelFile, true);
    }

    private double[] extractCVFeatures(CV cv) throws IOException {
        String cvText = (cv.getSkills() != null ? cv.getSkills() : "") + " " + (cv.getExperience() != null ? cv.getExperience() : "");
        return tfidfVectorizer.vectorize(cvText);
    }

    private double[] extractUserFeatures(User user) throws IOException {
        // Lấy CV của người dùng
        List<CV> cvs = cvRepository.findByUserId(user.getId());
        if (cvs.isEmpty()) {
            throw new RuntimeException("No CV found for user: " + user.getId());
        }

        // Chọn CV đầu tiên (có thể thay đổi logic để chọn CV mới nhất hoặc CV chính)
        CV cv = cvs.get(0);
        return extractCVFeatures(cv);
    }

    private double[] extractJobFeatures(Job job) throws IOException {
        String jobText = job.getJobDescription() + " " + job.getJobRequirement();
        return tfidfVectorizer.vectorize(jobText);
    }
}