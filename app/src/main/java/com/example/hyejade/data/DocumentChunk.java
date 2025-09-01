package com.example.hyejade.data;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.HnswIndex;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.VectorDistanceType;

@Entity
public class DocumentChunk {
    @Id public long id;

    public String docId;        // 원문 문서 식별자
    public int    chunkIndex;   // 문서 내 청크 순서

    public String text;

    // 모델 차원과 동일해야 함 (USE 예시: 512)
    @HnswIndex(
            dimensions = 512,
            distanceType = VectorDistanceType.COSINE
    )
    public float[] embedding;

    public DocumentChunk() {}
    public DocumentChunk(String docId, int idx, String text, float[] emb) {
        this.docId = docId;
        this.chunkIndex = idx;
        this.text = text;
        this.embedding = emb;
    }
}