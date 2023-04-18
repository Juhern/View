package com.example.myapplication;

import java.util.List;

public class AlBean {
    private List<Pdf> words_result;
    public static class Pdf {
        private String words;
        public String getWords() {
            return words;
        }
    }


    public List<Pdf> getWords_result() {
        return words_result;
    }
}
