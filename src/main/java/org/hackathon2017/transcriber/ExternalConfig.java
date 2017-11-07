package org.hackathon2017.transcriber;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "external")
public class ExternalConfig {

    private ExternalResource search;
    private ExternalResource transformer;
    private ExternalResource sphinxTranscriber;
    private ExternalResource storage;
    private ExternalResource storageService;

    public ExternalConfig() {
    }

    public ExternalResource getStorageService() {
        return storageService;
    }

    public void setStorageService(ExternalResource storageService) {
        this.storageService = storageService;
    }

    public ExternalResource getSearch() {
        return search;
    }

    public void setSearch(ExternalResource search) {
        this.search = search;
    }

    public ExternalResource getTransformer() {
        return transformer;
    }

    public void setTransformer(ExternalResource transformer) {
        this.transformer = transformer;
    }

    public ExternalResource getSphinxTranscriber() {
        return sphinxTranscriber;
    }

    public void setSphinxTranscriber(ExternalResource sphinxTranscriber) {
        this.sphinxTranscriber = sphinxTranscriber;
    }

    public ExternalResource getStorage() {
        return storage;
    }

    public void setStorage(ExternalResource storage) {
        this.storage = storage;
    }

    public static class ExternalResource {

        private String url;

        public ExternalResource() {
        }

        public ExternalResource(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String toPath(String path) {
            return this.url + path;
        }
    }


}
