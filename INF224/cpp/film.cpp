#include "film.h"

Film::Film() : Video(), chapiterNum(0), chapiters(nullptr) {}
Film::Film(int chapiterNum, int *chapiters) : chapiterNum(chapiterNum){
    this->setChapiters(chapiters);
}
Film::Film(string name, string path, double length, int chapiterNum, int *chapiters) : Video(name, path, length), chapiterNum(chapiterNum), chapiters(chapiters) {}

Film::~Film() {
    cout << "Film object: " << getName() << " deleted !" << endl;
}

int Film::getChapiterNum() const {
    return chapiterNum;
}

int *Film::getChapiters() const {
    return chapiters;
}

void Film::setChapiterNum(int chapiterNum) {
    this->chapiterNum = chapiterNum;
}

void Film::setChapiters(int *mychapiters) {
    if (! this->chapiterNum) {
        cerr << "Error: chapiterNum is not initialized !" << endl;
    }
    // this->chapiters = mychapiters;
    delete [] chapiters;
    if(mychapiters) {
        chapiters = new int[this->chapiterNum];
        for (int i = 0; i < this->chapiterNum; i++) {
            this->chapiters[i] = mychapiters[i];
        }
    } else {
        chapiters = nullptr;
    }
}

void Film::setChapiterNumAndChapiters(int chapiterNum, int *chapiters) {
    this->setChapiterNum(chapiterNum);
    this->setChapiters(chapiters);
}

void Film::display(std::ostream& ostrm) const {
        ostrm << "Film object:" << "-";
        ostrm << "  Name: " << getName() << "-";
        ostrm << "  Path: " << getPath() << "-";
        ostrm << "  length: " << getLength() << "-";
        ostrm << "  chapiterNum: " << chapiterNum << "-";
        ostrm << "  chapiters: ";
        for (int i = 0; i < chapiterNum; i++) {
            ostrm << chapiters[i] << " ";
        }
        ostrm << "-";
};

void Film::write(std::ostream& ostrm) const {
        ostrm << "Film object:" << endl;
        ostrm << getName() << endl;
        ostrm << getPath() << endl;
        ostrm << getLength() << endl;
        ostrm << chapiterNum << endl;
        for (int i = 0; i < chapiterNum; i++) {
            ostrm << chapiters[i] << endl;
        }
};

void Film::read(std::istream& istrm) {
    string name;
    string path;
    getline(istrm, name);
    getline(istrm, path);
    setName(name);
    setPath(path);
    string length;
    getline(istrm, length);
    setLength(stod(length));

    int *chapiters;
    string chapiterNum;
    getline(istrm, chapiterNum);
    int chapiterNumInt = stod(chapiterNum);
    chapiters = new int[chapiterNumInt];
    for (int i = 0; i < chapiterNumInt; i++) {
        string it;
        getline(istrm, it);
        chapiters[i] = stod(it);
    }
    
    setChapiterNumAndChapiters(chapiterNumInt, chapiters);
    delete [] chapiters;
};
