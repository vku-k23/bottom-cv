package com.cnpm.bottomcv.ai;

import com.cnpm.bottomcv.constant.AppConstant;
import com.cnpm.bottomcv.model.CV;
import com.cnpm.bottomcv.model.Job;
import com.cnpm.bottomcv.model.User;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Component
@RequiredArgsConstructor
public class TFIDFVectorizer {

    private final StandardAnalyzer analyzer = new StandardAnalyzer();
    private final Directory directory;
    private final Map<String, Integer> termToIndex = new HashMap<>();
    private final List<String> allTerms = new ArrayList<>();

    public TFIDFVectorizer() throws IOException {
        this.directory = FSDirectory.open(Paths.get("lucene-index"));
    }

    // Xây dựng chỉ mục từ dữ liệu người dùng và công việc
    public void buildIndex(List<User> users, List<Job> jobs) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter writer = new IndexWriter(directory, config)) {
            indexUserCVs(writer, users);
            indexJobs(writer, jobs);
        }

        buildTermDictionary();
    }

    private void indexUserCVs(IndexWriter writer, List<User> users) throws IOException {
        for (User user : users) {
            List<CV> userCVs = user.getCvs();
            if (userCVs != null) {
                for (CV cv : userCVs) {
                    String cvContent = buildCVContent(cv);
                    Document doc = createLuceneDoc(cvContent, "user", user.getId().toString());
                    writer.addDocument(doc);
                }
            }
        }
    }

    private void indexJobs(IndexWriter writer, List<Job> jobs) throws IOException {
        for (Job job : jobs) {
            String jobContent = buildJobContent(job);
            Document doc = createLuceneDoc(jobContent, "job", job.getId().toString());
            writer.addDocument(doc);
        }
    }

    private String buildCVContent(CV cv) {
        String skills = cv.getSkills() != null ? cv.getSkills() : "";
        String experience = cv.getExperience() != null ? cv.getExperience() : "";
        return skills + " " + experience;
    }

    private String buildJobContent(Job job) {
        String description = job.getJobDescription() != null ? job.getJobDescription() : "";
        String requirement = job.getJobRequirement() != null ? job.getJobRequirement() : "";
        return description + " " + requirement;
    }

    private Document createLuceneDoc(String content, String type, String id) {
        Document doc = new Document();
        FieldType fieldType = new FieldType(TextField.TYPE_STORED);
        fieldType.setStoreTermVectors(true); // Bắt buộc để lấy được term vectors
        Field contentField = new Field(AppConstant.FIELD_CONTENT, content, fieldType);
        doc.add(contentField);
        doc.add(new StringField("type", type, Field.Store.YES));
        doc.add(new StringField("id", id, Field.Store.YES));
        return doc;
    }

    private void buildTermDictionary() throws IOException {
        try (IndexReader reader = DirectoryReader.open(directory)) {
            for (int i = 0; i < reader.maxDoc(); i++) {
                Terms terms = reader.getTermVector(i, AppConstant.FIELD_CONTENT);
                if (terms == null)
                    continue;
                TermsEnum termsEnum = terms.iterator();
                BytesRef term;
                while ((term = termsEnum.next()) != null) {
                    String termText = term.utf8ToString();
                    if (!termToIndex.containsKey(termText)) {
                        termToIndex.put(termText, allTerms.size());
                        allTerms.add(termText);
                    }
                }
            }
        }
    }

    public double[] vectorize(String text) throws IOException {
        double[] tfidfVector = new double[allTerms.size()];
        Map<String, Integer> termFreq = new HashMap<>();

        // Phân tích văn bản để lấy TF
        TokenStream stream = analyzer.tokenStream(AppConstant.FIELD_CONTENT, text);
        stream.reset();
        while (stream.incrementToken()) {
            String term = stream.getAttribute(org.apache.lucene.analysis.tokenattributes.CharTermAttribute.class)
                    .toString();
            termFreq.put(term, termFreq.getOrDefault(term, 0) + 1);
        }
        stream.end();
        stream.close();

        try (IndexReader reader = DirectoryReader.open(directory)) {
            int totalTerms = termFreq.values().stream().mapToInt(Integer::intValue).sum();

            for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
                String term = entry.getKey();
                int freq = entry.getValue();
                if (termToIndex.containsKey(term)) {
                    int termIndex = termToIndex.get(term);
                    double tf = (double) freq / totalTerms;
                    double idf = Math.log((double) reader.numDocs() /
                            (reader.docFreq(new Term(AppConstant.FIELD_CONTENT, term)) + 1)) + 1;
                    tfidfVector[termIndex] = tf * idf;
                }
            }
        }

        return tfidfVector;
    }

    public int getFeatureSize() {
        return allTerms.size();
    }
}
