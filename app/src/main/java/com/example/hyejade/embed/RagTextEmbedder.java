package com.example.hyejade.embed;

public interface RagTextEmbedder extends AutoCloseable {
    int    dimensions();
    float[] embed(String text);
    @Override void close();
}