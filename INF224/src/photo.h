/*
 * @brief: subclass of Multimedia, to specify the propotes of the photo which include two parameters
 */
#ifndef PHOTO_H
#define PHOTO_H

#include <iostream>
#include <string>
#include "multimedia.h"

class Photo : public Multimedia
{
private:
    double latitude;
    double longitude;

public:
/*
* Constructor of the class Photo with parameters
* @param name: name of the photo
* @param path: path of the photo
* @param latitude: latitude of the photo
* @param longitude: longitude of the photo
*/
    Photo() : latitude(0), longitude(0) {};
    Photo(double latitude, double longitude) : latitude(latitude), longitude(longitude) {};
    Photo(string name, string path, double latitude, double longitude) : Multimedia(name, path), latitude(latitude), longitude(longitude) {};

/*
 * Destructor of the class Photo
 */
    virtual ~Photo() {
        cout << "Photo object: " << getName() << " deleted !" << endl;
    };

/*
 * Getters and setters of the class Photo
 * @param latitude: latitude of the photo
 * @param longitude: longitude of the photo
 * @return latitude: latitude of the photo
 * @return longitude: longitude of the photo
 */
    double getLatitude() const { return latitude; };
    double getLongitude() const { return longitude; };
    void setLatitude(double latitude) { this->latitude = latitude; };
    void setLongitude(double longitude) { this->longitude = longitude; };

/*
 * Display the photo object with the name, the path, the latitude and the longitude
 * @param ostrm: output stream
 */
    virtual void display(std::ostream& ostrm) const {
        ostrm << "Photo object:" << endl;
        ostrm << "  Name: " << getName() << endl;
        ostrm << "  Path: " << getPath() << endl;
        ostrm << "  latitude: " << latitude << endl;
        ostrm << "  logitude: " << longitude << endl;
    };
    
/*
 * Play the photo object with the imagej
 */
    virtual void play() const {
        // string cmd = "imagej " + getPath() + getName() + " &";
        string cmd = "open " + getPath() + getName();
        system(cmd.c_str());
    };
};

#endif // PHOTO_H
