//
// main.cpp
// Created on 21/10/2018
//

#include <cstdlib>
#include <iostream>
#include <memory>
#include <sstream>
#include <string>
#include "multimedia.h"
#include "photo.h"
#include "video.h"
#include "film.h"
#include "group.h"
#include "multimediamanager.h"

#define SKT
#define DEBUG


#ifdef SKT
#include "tcpserver.h"
const int PORT = 3331;
#endif


int main(int argc, const char* argv[])
{
#ifdef DEBUG
    // step 4
    // Photo *p = new Photo("test.png", "./ressource/", 10, 10);
    // p->setLatitude(25);
    // p->setLongitude(25);
    // p->display(std::cout);
    // p->play();

    // Video *v = new Video("test.mp4", "ressource/", 10);
    // v->setLength(20);
    // v->display(std::cout);
    // v->play();

    // step 5
    // Multimedia **tab = new Multimedia*[2];
    // int cnt = 0;
    // tab[cnt++] = new Photo("test.png", "./ressource/", 10, 10);
    // tab[cnt++] = new Video("test.mp4", "./ressource/", 10);
    // for (int i = 0; i < cnt; i++) {
    //     tab[i]->display(std::cout);
    //     tab[i]->play();
    // }

    // step 6 & 7 
    // Still have problem in 7
    // int *tab = new int[2]();
    // tab[0] = 10; tab[1] = 20;
    // Film *f = new Film("test.mp4", "./ressource/", 10, 2, tab);
    // f->display(std::cout);

    // for (int i = 0; i < 2; i++) {
    //     tab[i] += 1;
    // }
    // f->setChapiters(tab);
    // f->display(std::cout);

    // delete [] tab;
    // tab = (int*)malloc(3*sizeof(int));
    // tab[0] = 15; tab[1] = 25; tab[2] = 35;
    // f->setChapiterNumAndChapiters(3, tab);
    // f->display(std::cout);

    // Film *f2 = new Film();
    // f2 = f;
    // f2->display(std::cout);
    // delete f;
    // f = new Film();
    // f->setChapiterNum(1);
    // tab[0] = 100;
    // f->setChapiters(tab);
    // f2->display(std::cout);

    // step 8 & 9
    // Group *g1 = new Group("group1");
    // Group *g2 = new Group("group2");
    // shared_ptr<Multimedia> sharedPhoto(new Photo("test.png", "./ressource/", 10, 10));
    // shared_ptr<Multimedia> sharedVideo(new Video("test.mp4", "./ressource/", 10));

    // g1->insertObject(sharedPhoto);
    // g1->insertObject(sharedVideo);
    // g2->insertObject(sharedPhoto);
    // g1->display(std::cout);
    // g2->display(std::cout);

    // g1->removeObject(sharedPhoto);
    // g1->display(std::cout);
    // g2->display(std::cout);

    // g2->removeObject(sharedPhoto);
    // g2->display(std::cout);
    // delete g2;
    // g1->removeObject(sharedVideo);
    // g1->display(std::cout);
    // sharedPhoto.reset();
    // sharedVideo.reset();

    // delete g1;

    // step 10
    // int *tab = new int[2]();
    // tab[0] = 14; tab[1] = 15;
    // MultimediaManager *manager = new MultimediaManager();
    // shared_ptr<Photo> photo = manager->createPhoto("test.png", "./ressource/", 10, 11);
    // shared_ptr<Video> video = manager->createVideo("test.mp4", "./ressource/", 12);
    // shared_ptr<Film> film = manager->createFilm("test.mkv", "./ressource/", 13, 2, tab);
    // shared_ptr<Group> group = manager->createGroup("group");
    // group->insertObject(photo);
    // group->insertObject(video);
    // group->insertObject(film);
    // manager->findMultimedia(std::cout, "test.png");
    // manager->findMultimedia(std::cout, "test.mp4");
    // manager->findMultimedia(std::cout, "test.mkv");
    // manager->findGroup(std::cout, "group");

    // // manager->playMultimedia("test.png");
    // // manager->playMultimedia("test.mp4");
    // // manager->playMultimedia("test.mkv");

    // manager->deleteMultimedia("test.png");
    // manager->findMultimedia(std::cout, "test.png");
    // manager->findGroup(std::cout, "group");
    // manager->deleteGroup("group");
    // manager->findGroup(std::cout, "group");

    // step 11
#ifdef SKT
    int *tab = new int[2]();
    tab[0] = 14; tab[1] = 15;
    MultimediaManager *manager = new MultimediaManager();
    shared_ptr<Photo> photo = manager->createPhoto("test.png", "./ressource/", 10, 11);
    shared_ptr<Video> video = manager->createVideo("test.mp4", "./ressource/", 12);
    shared_ptr<Film> film = manager->createFilm("test.mkv", "./ressource/", 13, 2, tab);
    // shared_ptr<Photo> photo = manager->createMultimedia<Photo>("test.png", "./ressource/", new Photo("test.png", "./ressource/", 10, 11));
    // shared_ptr<Video> video = manager->createMultimedia<Video>("test.mp4", "./ressource/", new Video("test.mp4", "./ressource/", 12));
    // shared_ptr<Film> film = manager->createMultimedia<Film>("test.mkv", "./ressource/", new Film("test.mkv", "./ressource/", 13, 2, tab));
    shared_ptr<Group> group = manager->createGroup("group");
    group->insertObject(photo);
    group->insertObject(video);
    group->insertObject(film);

    // cree le TCPServer
    auto* server = 
    new TCPServer( [&](std::string const& request, std::string& response) {
        // // the request sent by the client to the server
        // std::cout << "request: " << request << std::endl;
        // // the response that the server sends back to the client
        // response = "RECEIVED: " + request;
        // // return false would close the connecytion with the client
        // return true;
        istringstream istrstream(request);
        string cmd;
        istrstream >> cmd;
        response = request + "Success !";

        if (cmd == "Search" || cmd == "search") {
            string arg2;
            istrstream >> arg2;
            if (arg2 == "Group" || arg2 == "group") {
                string groupName;
                istrstream >> groupName;
                manager->findGroup(std::cout, groupName);
            } else {
                manager->findMultimedia(std::cout, arg2);
            }
        } else if (cmd == "Play" || cmd == "play") {
            string name;
            istrstream >> name;
            manager->playMultimedia(name);
        } else {
            response = "Command not found";
        }
        return true;
    });
    // lance la boucle infinie du serveur
    std::cout << "Starting Server on port " << PORT << std::endl;
    int status = server->run(PORT);
    // en cas d'erreur
    if (status < 0) {
        std::cerr << "Could not start Server on port " << PORT << std::endl;
        return 1;
    }
#endif
#endif

    return 0;
}
