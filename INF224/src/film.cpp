#include "film.h"

/*
 * Constructor of the class Film with parameters
 * @param chapiterNum: number of chapiters
 * @param chapiters: array of chapiters
 */
Film::Film() : Video(), chapiterNum(0), chapiters(nullptr) {}
Film::Film(int chapiterNum, int *chapiters) : chapiterNum(chapiterNum){
    this->setChapiters(chapiters);
}
Film::Film(string name, string path, double length, int chapiterNum, int *chapiters) : Video(name, path, length), chapiterNum(chapiterNum), chapiters(chapiters) {}

/*
 * Destructor of the class Film
 */
Film::~Film() {
    cout << "Film object: " << getName() << " deleted !" << endl;
}

/*
 * Getters and setters of the class Film
 * @param chapiterNum: number of chapiters
 * @param chapiters: array of chapiters
 */
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

/*
 * Display the multimedia object with the name and the path
 * @param ostrm: output stream
 */
void Film::display(std::ostream& ostrm) const {
        ostrm << "Film object:" << endl;
        ostrm << "  Name: " << getName() << endl;
        ostrm << "  Path: " << getPath() << endl;
        ostrm << "  length: " << getLength() << endl;
        ostrm << "  chapiterNum: " << chapiterNum << endl;
        ostrm << "  chapiters: ";
        for (int i = 0; i < chapiterNum; i++) {
            ostrm << chapiters[i] << " ";
        }
        ostrm << endl;
    };
