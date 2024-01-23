#ifndef FILM_H
#define FILM_H

#include "video.h"
#include <ostream>

/// subclass of Video, with additional parameters: chapiter
class Film : public Video
{
private:
    int chapiterNum;
    int *chapiters;
public:

    Film();

/// @param chapiterNum: number of chapiters
/// @param chapiters: array of chapiters
    Film(int chapiterNum, int *chapiters);

/// @param name: name of the film
/// @param path: path of the film
/// @param length: length of the film
/// @param chapiterNum: number of chapiters
/// @param chapiters: array of chapiters
    Film(string name, string path, double length, int chapiterNum, int *chapiters);


    virtual ~Film();

    int getChapiterNum() const;
    int *getChapiters() const;

/// @param chapiterNum: number of chapiters
    void setChapiterNum(int chapiterNum);

/// @param chapiters: array of chapiters    
    void setChapiters(int *chapiters);

/// @param chapiterNum: number of chapiters
/// @param chapiters: array of chapiters
    void setChapiterNumAndChapiters(int chapiterNum, int *chapiters);

/// Display the film object with the name, path, length, number of chapiters and chapiters
/// @param ostrm: output stream
    void display(std::ostream& ostrm) const override;

/// Write the film object to the output stream
/// @param ostrm: output stream
    void write(std::ostream& ostrm) const override;

/// Read the film object from the input stream
/// @param istrm: input stream
    void read(std::istream& istrm) override;
};

#endif // FILM_H
