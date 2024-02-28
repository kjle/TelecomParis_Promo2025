"use strict";

import {fibIt,fibRec,fibArr,fiboMap} from "./exercise1.mjs";
console.log('---q1---');
console.log(fibIt(7)); // do more that one test per function
console.log(fibRec(8));
console.log(fibArr([3,5]));
console.log(fiboMap([4,6]));

import {wcount,WList} from "./exercise2.mjs";
console.log('---q2---');
console.log(wcount("fish bowl fish bowl fish"));
let wl = new WList("fish bowl fish bowl fish");
console.log(wl.getWords());
console.log(wl.maxCountWord());
console.log(wl.minCountWord());
console.log(wl.getCount("fish"));
function f(s) {return s.length;}
console.log(wl.applyWordFunc(f));

import { Student, ForStudent } from "./exercise3.mjs";
console.log('---q3---');
var student = new Student("Dupond", "John", 1835);
console.log(student.toString());
var forStudent = new ForStudent("Dupond2", "John2", 1837, "French");
console.log(forStudent.toString());

import { Prmtn } from "./exercise4.mjs";
console.log('---q4---');
var prmtn = new Prmtn();
prmtn.add(student);
prmtn.add(forStudent);
console.log(prmtn.size());
console.log(prmtn.get(0).toString());
console.log(prmtn.get(1).toString());
console.log(prmtn.print());
console.log(prmtn.wirte());
prmtn.saveFile("test.json");
prmtn.readFile("test.json");
