/*
 * @brief: subclass of Multimedia, to specify the propotes of the video which include one parameter
 */
#ifndef VIDEO_H
#define VIDEO_H

#include <iostream>
#include <string>
#include "multimedia.h"

class Video : public Multimedia
{
private:
    double length;

public:
/*
* Constructor of the class Video with parameters
* @param name: name of the video
* @param path: path of the video
* @param length: length of the video
*/
    Video() : length(0) {};
    Video(double length) : length(length) {};
    Video(string name, string path, double length) : Multimedia(name, path), length(length) {};

/*
 * Destructor of the class Video
 */
    virtual ~Video() {
        cout << "Video object: " << getName() << " deleted !" << endl;
    };

/*
 * Getters and setters of the class Video
 * @param length: length of the video
 * @return length: length of the video
 */
    double getLength() const { return length; };
    void setLength(double length) { this->length = length; };

/*
 * Display the video object with the name, the path, and the length
 * @param ostrm: output stream
 */
    virtual void display(std::ostream& ostrm) const {
        ostrm << "Video object:" << endl;
        ostrm << "  Name: " << getName() << endl;
        ostrm << "  Path: " << getPath() << endl;
        ostrm << "  length: " << length << endl;
    };
    
/*
 * Play the video object with the mpv
 */
    virtual void play() const {
        // string cmd = "mpv " + getPath() + getName() + " &";
        string cmd = "open " + getPath() + getName();
        system(cmd.c_str());
    };

/*
 * Write the video object to the output stream
 * @param ostrm: output stream
 */
    virtual void write(std::ostream& ostrm) const {
        ostrm << "Video object:" << endl;
        ostrm << getName() << endl;
        ostrm << getPath() << endl;
        ostrm << length << endl;
    };

/*
 * Read the video object from the input stream
 * @param istrm: input stream
 */
    virtual void read(std::istream& istrm) {
        string name;
        string path;
        getline(istrm, name);
        setName(name);
        getline(istrm, path);
        setPath(path);
        string length;
        getline(istrm, length);
        setLength(stod(length));
    };
};
#endif // VIDEO_H
