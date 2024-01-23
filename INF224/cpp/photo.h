/*
 * @brief: subclass of Multimedia, to specify the propotes of the photo which include two parameters
 */
#ifndef PHOTO_H
#define PHOTO_H

#define Ubuntu

#include <iostream>
#include <string>
#include "multimedia.h"

/// @brief subclass of Multimedia, to specify the propotes of the photo which include two parameters
class Photo : public Multimedia
{
private:
    double latitude;
    double longitude;

public:

    Photo() : latitude(0), longitude(0) {};

/// @param latitude: latitude of the photo
/// @param longitude: longitude of the photo
    Photo(double latitude, double longitude) : latitude(latitude), longitude(longitude) {};

/// @param name: name of the photo
/// @param path: path of the photo
/// @param latitude: latitude of the photo
/// @param longitude: longitude of the photo
    Photo(string name, string path, double latitude, double longitude) : Multimedia(name, path), latitude(latitude), longitude(longitude) {};

    virtual ~Photo() {
        cout << "Photo object: " << getName() << " deleted !" << endl;
    };

/// @return latitude: latitude of the photo
    double getLatitude() const { return latitude; };
    
/// @return longitude: longitude of the photo    
    double getLongitude() const { return longitude; };
    
/// @param latitude: latitude of the photo    
    void setLatitude(double latitude) { this->latitude = latitude; };
    
/// @param longitude: longitude of the photo    
    void setLongitude(double longitude) { this->longitude = longitude; };

/// @brief display the photo object with the name, path, latitude and longitude
/// @param ostrm: output stream
    virtual void display(std::ostream& ostrm) const {
        ostrm << "Photo object:" << "-";
        ostrm << "  Name: " << getName() << "-";
        ostrm << "  Path: " << getPath() << "-";
        ostrm << "  latitude: " << latitude << "-";
        ostrm << "  logitude: " << longitude << "-";
    };
    
/// Play the photo object
    virtual void play() const {
        #if defined(Ubuntu)
        string cmd = "imagej " + getPath() + getName() + " &";
        #elif defined(Mac)
        string cmd = "open " + getPath() + getName();
        #else
        string cmd = "show " + getPath() + getName();
        #endif

        system(cmd.c_str());
    };

/// Write the photo object to the output stream
/// @param ostrm: output stream
    virtual void write(std::ostream& ostrm) const {
        ostrm << "Photo object:" << endl;
        ostrm << getName() << endl;
        ostrm << getPath() << endl;
        ostrm << latitude << endl;
        ostrm << longitude << endl;
    };

/// Read the photo object from the input stream
/// @param istrm: input stream
    void read(std::istream& istrm) {
        string name, path;
        getline(istrm, name);
        getline(istrm, path);
        setName(name);
        setPath(path);
        string latitude, longitude;
        getline(istrm, latitude);
        getline(istrm, longitude);
        setLatitude(stod(latitude));
        setLongitude(stod(longitude));
    };
};

#endif // PHOTO_H
