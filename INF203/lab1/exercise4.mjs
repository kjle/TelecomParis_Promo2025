"use strict";

import {Student, ForStudent} from "./exercise3.mjs";
import fs from 'fs';

export class Prmtn {
    constructor() {
        this.students = [];
    }

    add(student) {
        this.students.push(student);
    }

    size() {
        return this.students.length;
    }

    get(i) {
        return this.students[i];
    }

    print() {
        let str = '';
        for (let item of this.students) {
            str += item.toString() + '\n';
        }
        console.log(str);
        return str;
    }

    wirte() {
        return JSON.stringify(this.students);
    }

    read(str) {
        let tmp = JSON.parse(str);
        for (let item of tmp) {
            if (item.nationality === undefined) {
                this.add(new Student(item.lastName, item.firstName, item.id));
            } else {
                this.add(new ForStudent(item.lastName, item.firstName, item.id, item.nationality));
            }
        }
    }

    saveFile(fileName) {
        fs.writeFileSync(fileName, this.wirte());
    }

    readFile(fileName) {
        this.read(fs.readFileSync(fileName, 'utf8'));
    }
}
