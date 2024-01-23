/*
 * @brief: Abstract base class of the class heritage tree.
 */

#ifndef MULTIMEDIA_H
#define MULTIMEDIA_H

#include <iostream>
#include <string>

using namespace std;

/// @brief Abstract base class to define basic multimedia objects.
class Multimedia
{
private:
    string name;
    string path;
public:

    Multimedia();

/// @param name: name of the multimedia object
/// @param path: path of the multimedia object
    Multimedia(string name, string path);

    virtual ~Multimedia();

/// @return name: name of the multimedia object
    string getName() const;

/// @return path: path of the multimedia object
    string getPath() const;

/// @param name: name of the multimedia object
    void setName(string name);

/// @param path: path of the multimedia object
    void setPath(string path);

/// Display the multimedia object with the name and path
/// @param ostrm: output stream
    virtual void display(std::ostream& ostrm) const;

/// Play the multimedia object
    virtual void play() const = 0;

/// Write the multimedia object to the output stream
/// @param ostrm: output stream
    virtual void write(ostream& ostrm) const;

/// Read the multimedia object from the input stream
/// @param istrm: input stream
    virtual void read(istream& istrm);
};

#endif // MULTIMEDIA_H
