"use strict";

export function wcount(str) {
    let words = str.split(' ');
    let counts = {};

    for (let i = 0; i < words.length; i++) {
        let word = words[i];
        if (counts[word] === undefined) {
            counts[word] = 1;
        } else {
            counts[word]++;
        }
    }

    return counts;
}

export class WList {
    constructor(str) {
        this.words = str.split(' ');
        this.counts = wcount(str);
    }

    getWords() {
        this.uniqueWords = new Set();
        for (let word of this.words) {
            this.uniqueWords.add(word);
        }
        return Array.from(this.uniqueWords).sort();
    }

    maxCountWord() {
        let sortedWords = this.getWords();
        let maxCount = 0;
        let maxWord = sortedWords[0];
        for (let word of sortedWords) {
            if (this.counts[word] > maxCount) {
                maxCount = this.counts[word];
                maxWord = word;
            }
        }
        return maxWord;
    }

    minCountWord() {
        let sortedWords = this.getWords();
        let minCount = Infinity;
        let minWord = sortedWords[0];

        for (let word of sortedWords) {
            if (this.counts[word] < minCount) {
                minCount = this.counts[word];
                minWord = word;
            }
        }
        return minWord;
    }

    getCount(word) {
        return this.counts[word] || 0;
    }

    applyWordFunc(f) {
        let result = [];
        for (let word of this.getWords()) {
            result.push(f(word));
        }
        return result;
    }
}