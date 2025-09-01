package com.example.hyejade.vec;

import com.example.hyejade.App;
import com.example.hyejade.data.DocumentChunk;
import com.example.hyejade.embed.RagTextEmbedder;
import io.objectbox.Box;

public class ChunkIndexer {
    private final Box<DocumentChunk> box;
    private final RagTextEmbedder embedder;

    public ChunkIndexer(RagTextEmbedder embedder) {
        this.box = App.getStore().boxFor(DocumentChunk.class);
        this.embedder = embedder;
    }

    public void indexDocument(String docId, String text, int chunkSize, int overlap) {
        int n = text.length(), start = 0, idx = 0;
        while (start < n) {
            int end = Math.min(n, start + chunkSize);
            String chunk = text.substring(start, end);
            float[] emb = embedder.embed(chunk);
            box.put(new DocumentChunk(docId, idx++, chunk, emb));
            if (end == n) break;
            start = Math.max(0, end - overlap);
        }
    }
}