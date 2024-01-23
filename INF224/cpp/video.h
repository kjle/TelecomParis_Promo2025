/*
 * @brief: subclass of Multimedia, to specify the propotes of the video which include one parameter
 */
#ifndef VIDEO_H
#define VIDEO_H

#define Ubuntu

#include <iostream>
#include <string>
#include "multimedia.h"

/// @brief subclass of Multimedia, to specify the propotes of the video which include one parameter
class Video : public Multimedia
{
private:
    double length;

public:

    Video() : length(0) {};

/// @param length: length of the video
    Video(double length) : length(length) {};

/// @param name: name of the video
/// @param path: path of the video
/// @param length: length of the video    
    Video(string name, string path, double length) : Multimedia(name, path), length(length) {};

    virtual ~Video() {
        cout << "Video object: " << getName() << " deleted !" << endl;
    };

/// @return length: length of the video
    double getLength() const { return length; };
    
/// @param length: length of the video    
    void setLength(double length) { this->length = length; };

/// @brief display the video object with the name, path and length
/// @param ostrm: output stream
    virtual void display(std::ostream& ostrm) const {
        ostrm << "Video object:" << "-";
        ostrm << "  Name: " << getName() << "-";
        ostrm << "  Path: " << getPath() << "-";
        ostrm << "  length: " << length << "-";
    };
    
/// Play the video object
    virtual void play() const {
        #if defined(Ubuntu)
        string cmd = "mpv " + getPath() + getName() + " &";
        #elif defined(Mac)
        string cmd = "open " + getPath() + getName();
        #else
        string cmd = "show " + getPath() + getName();
        #endif

        system(cmd.c_str());
    };

/// Write the video object to the output stream
/// @param ostrm: output stream
    virtual void write(std::ostream& ostrm) const {
        ostrm << "Video object:" << endl;
        ostrm << getName() << endl;
        ostrm << getPath() << endl;
        ostrm << length << endl;
    };

/// Read the video object from the input stream
/// @param istrm: input stream
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
