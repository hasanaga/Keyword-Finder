package com.keywordfinder;


import java.util.ArrayList;
import java.util.List;

public  class Keyword {
    String word;
    int count;
    List<KeywordSource> keywordSources;

    public Keyword(String word) {
        this.word = word;
        this.count = 1;
        keywordSources = new ArrayList<>();
    }

    public void addSource(KeywordSource keywordSource){
        keywordSources.add(keywordSource);
    }


    public int wordCount(){
        return  word.trim().split(" ").length;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((word == null) ? 0 : word.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Keyword other = (Keyword) obj;
        if (word == null) {
            if (other.word != null)
                return false;
        } else if (!word.equals(other.word))
            return false;
        return true;
    }


}

