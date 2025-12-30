-- SQL script to drop the DOCUMENT_EMBEDDINGS table
-- Run this manually in your Oracle database if you want to drop the table before restarting the app
-- Then change CreateOption back to CREATE_IF_NOT_EXISTS in AiConfig.java

DROP TABLE DOCUMENT_EMBEDDINGS CASCADE CONSTRAINTS;

