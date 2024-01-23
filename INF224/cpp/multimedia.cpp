#include "multimedia.h"
#include <iostream>
#include <string>


Multimedia::Multimedia() {}

Multimedia::Multimedia(string name, string path) : name(name), path(path) {}

Multimedia::~Multimedia() {
    cout << "Multimedia object: " << name << " deleted !" << endl;
}

string Multimedia::getName() const {
    return name;
}

string Multimedia::getPath() const {
    return path;
}

void Multimedia::setName(string name) {
    this->name = name;
}

void Multimedia::setPath(string path) {
    this->path = path;
}

void Multimedia::display(ostream& ostrm) const {
    ostrm << "Name: " << name << endl;
    ostrm << "Path: " << path << endl;
}

void Multimedia::write(ostream& ostrm) const {
    ostrm << "Multimedia object:" << endl;
    ostrm << name << endl;
    ostrm << path << endl;
}

void Multimedia::read(istream& istrm) {
    getline(istrm, this->name);
    getline(istrm, this->path);
}
