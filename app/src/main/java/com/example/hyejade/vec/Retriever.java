package com.example.hyejade.vec;

import com.example.hyejade.App;
import com.example.hyejade.data.DocumentChunk;
import io.objectbox.Box;
import io.objectbox.query.ObjectWithScore;
import io.objectbox.query.Query;
import java.util.ArrayList;
import java.util.List;
import com.example.hyejade.data.DocumentChunk_;

public class Retriever {
    public static class Hit {
        public final String docId;
        public final int    chunkIndex;
        public final String text;
        public final double distance; // 작을수록 유사
        public Hit(String d, int i, String t, double dist) {
            docId=d; chunkIndex=i; text=t; distance=dist;
        }
    }

    private final Box<DocumentChunk> box;

    public Retriever() {
        this.box = App.getStore().boxFor(DocumentChunk.class);
    }

    public List<Hit> topK(float[] queryVec, int k) {
        Query<DocumentChunk> q = box.query(
                DocumentChunk_.embedding.nearestNeighbors(queryVec, k)
        ).build();
        List<ObjectWithScore<DocumentChunk>> rs = q.findWithScores();
        q.close();

        List<Hit> out = new ArrayList<>(rs.size());
        for (var r : rs) {
            DocumentChunk c = r.get();
            out.add(new Hit(c.docId, c.chunkIndex, c.text, r.getScore()));
        }
        return out;
    }
}