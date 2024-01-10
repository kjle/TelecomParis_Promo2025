#include "multimedia.h"
#include <iostream>
#include <string>

/*
 * Constructor of the class Multimedia
 */
Multimedia::Multimedia() {}

/* 
 * Constructor of the class Multimedia with parameters
 * @param name: name of the multimedia object
 * @param path: path of the multimedia object
 */
Multimedia::Multimedia(string name, string path) : name(name), path(path) {}

/*
 * Destructor of the class Multimedia
 */
Multimedia::~Multimedia() {
    cout << "Multimedia object: " << name << " deleted !" << endl;
}

/*
 * Getters and setters of the class Multimedia
 * @return name: name of the multimedia object
 */
string Multimedia::getName() const {
    return name;
}

/*
 * Getters and setters of the class Multimedia
 * @return path: path of the multimedia object
 */
string Multimedia::getPath() const {
    return path;
}

/*
 * Getters and setters of the class Multimedia
 * @param name: name of the multimedia object
 */
void Multimedia::setName(string name) {
    this->name = name;
}

/*
 * Getters and setters of the class Multimedia
 * @param path: path of the multimedia object
 */
void Multimedia::setPath(string path) {
    this->path = path;
}

/*
 * Display the multimedia object with the name and the path
 * @param ostrm: output stream
 */
void Multimedia::display(ostream& ostrm) const {
    ostrm << "Name: " << name << endl;
    ostrm << "Path: " << path << endl;
}

/*
 * Write the multimedia object to the output stream
 * @param ostrm: output stream
 */
void Multimedia::write(ostream& ostrm) const {
    ostrm << "Multimedia object:" << endl;
    ostrm << name << endl;
    ostrm << path << endl;
}

/*
 * Read the multimedia object from the input stream
 * @param istrm: input stream
 */
void Multimedia::read(istream& istrm) {
    getline(istrm, this->name);
    getline(istrm, this->path);
}


