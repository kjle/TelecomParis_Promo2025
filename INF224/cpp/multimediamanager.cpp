#include "multimediamanager.h"
#include "photo.h"
#include <fstream>
#include <memory>

MultimediaManager::MultimediaManager() : multimediaTable(), groupTable(){};

MultimediaManager::~MultimediaManager() {
}

shared_ptr<Photo> MultimediaManager::createPhoto(string name, string path, double latitude, double longitude) {
    shared_ptr<Photo> photo = make_shared<Photo>(name, path, latitude, longitude);
    multimediaTable[name] = photo;
    return photo;
}

shared_ptr<Video> MultimediaManager::createVideo(string name, string path, int duration) {
    shared_ptr<Video> video = make_shared<Video>(name, path, duration);
    multimediaTable[name] = video;
    return video;
}

shared_ptr<Film> MultimediaManager::createFilm(string name, string path, int duration, int chapiterNum, int *chapiters) {
    shared_ptr<Film> film = make_shared<Film>(name, path, duration, chapiterNum, chapiters);
    multimediaTable[name] = film;
    return film;
}

shared_ptr<Group> MultimediaManager::createGroup(string name) {
    shared_ptr<Group> group = make_shared<Group>(name);
    groupTable[name] = group;
    return group;
}

void MultimediaManager::findMultimedia(ostream& ostr, string name) {
    auto it = multimediaTable.find(name);
    if (it != multimediaTable.end()) {
        it->second->display(ostr);
    } else {
        ostr << "Multimedia " << name << " not found !" << endl;
    }
}

void MultimediaManager::findGroup(ostream& ostr, string name) {
    auto it = groupTable.find(name);
    if (it != groupTable.end()) {
        it->second->display(ostr);
    } else {
        ostr << "Group " << name << " not found !" << endl;
    }
}

void MultimediaManager::playMultimedia(string name) {
    auto it = multimediaTable.find(name);
    if (it != multimediaTable.end()) {
        it->second->play();
    } else {
        cout << "Multimedia " << name << " not found !" << endl;
    }
}

void MultimediaManager::deleteMultimedia(string name) {
    auto it = multimediaTable.find(name);
    if (it != multimediaTable.end()) {
        for (auto &group : groupTable) {
            group.second->removeObject(it->second);
        }
        multimediaTable.erase(it);
    } else {
        cout << "Multimedia " << name << " not found !" << endl;
    }
}

void MultimediaManager::deleteGroup(string name) {
    auto it = groupTable.find(name);
    if (it != groupTable.end()) {
        groupTable.erase(it);
    } else {
        cout << "Group " << name << " not found !" << endl;
    }
}

void MultimediaManager::saveMultimedia(const string &outputFile) {
    ofstream ofs(outputFile);
    if(ofs) {
        for (auto it = multimediaTable.begin(); it != multimediaTable.end(); ++it) {
            it->second->write(ofs);
        }
        ofs.close();
    } else {
        cerr << "Error: cannot open the file " << outputFile << endl;
    }
}

void MultimediaManager::loadMultimedia(const string &inputFile) {
    ifstream ifs(inputFile);
    if(ifs) {
        string type;
        while(getline(ifs, type)) {
            if (type == "Photo object:") {
                // Photo *r = new Photo();
                // r->read(ifs);
                string name, path;
                getline(ifs, name);
                getline(ifs, path);
                string latitude, longitude;
                getline(ifs, latitude);
                getline(ifs, longitude);
                // Photo *element = new Photo(name, path, stod(latitude), stod(longitude));
                shared_ptr<Photo> photo = createPhoto(name, path, stod(latitude), stod(longitude));

            } else if (type == "Video object:") {
                // Video *r = new Video();
                // r->read(ifs);
                string name;
                string path;
                getline(ifs, name);
                getline(ifs, path);
                string length;
                getline(ifs, length);
                shared_ptr<Video> video = createVideo(name, path, stoi(length));
            } else if (type == "Film object:") {
                // Film *r = new Film();
                // r->read(ifs);
                string name;
                string path;
                getline(ifs, name);
                getline(ifs, path);
               
                string length;
                getline(ifs, length);

                // int *chapiters;
                string chapiterNum;                

                getline(ifs, chapiterNum);
                int chapiterNumInt = stod(chapiterNum);
                int *chapiters = new int[chapiterNumInt]();
                // int *tab = new int[]();
                int tab[10];
                for (int i = 0; i < chapiterNumInt; i++) {
                    string it;
                    getline(ifs, it);
                    cout << "getline: " << it << endl;
                    // chapiters[i] = stod(it);
                    // (*(chapiters + i)) = stod(it);
                    tab[i] = stod(it);
                }
                shared_ptr<Film> film = createFilm(name, path, stod(length), chapiterNumInt, tab);
                cout << "test for chapiters:" << endl;
                for (int i = 0; i < chapiterNumInt; i++) {
                    cout << chapiters[i] << " ";
                }
                delete [] chapiters;
            }
        }
        
        ifs.close();
    } else {
        cerr << "Error: cannot open the file " << inputFile << endl;
    }

}
