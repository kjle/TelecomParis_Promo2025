#include "multimediamanager.h"

MultimediaManager::MultimediaManager() : multimediaTable(), groupTable(){};

MultimediaManager::~MultimediaManager() {
}

// template<typename T> shared_ptr<T> MultimediaManager::createMultimedia(string name, string path, T *multimedia) {
//     shared_ptr<T> multimediaPtr = make_shared<T>(name, path, multimedia);
//     multimediaTable[name] = multimediaPtr;
//     return multimediaPtr;
// }

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