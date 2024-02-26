"use strict";

// non recursive
export function fibIt(n) {
    let a = 0, b = 1, temp;
    for (let i = 0; i < n; i++) {
        temp = a;
        a = b;
        b = temp + b;
    }
    return a;
}

// recursive function
export function fibRec(n) {
    if (n <= 1) return n;
    return fibRec(n - 1) + fibRec(n - 2);
}

// process array with a loop
export function fibArr(t) {
    let res = [];
    for (let i = 0; i < t.length; i++) {
        res.push(fibIt(t[i]));
    }
    return res;
}

// use of map
export function fiboMap(t) {
    return t.map(fibIt);
}