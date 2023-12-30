/*
 * @brief: Abstract base class of the class heritage tree.
 */

#ifndef MULTIMEDIA_H
#define MULTIMEDIA_H

#include <iostream>
#include <string>

using namespace std;

class Multimedia
{
private:
    string name;
    string path;
public:
    /*
     * Constructor of the class Multimedia with parameters
     * @param name: name of the multimedia object
     * @param path: path of the multimedia object
     */
    Multimedia();
    Multimedia(string name, string path);

    /*
     * Destructor of the class Multimedia
     */
    virtual ~Multimedia();

    /*
     * Getters and setters of the class Multimedia
     * @return name: name of the multimedia object
     */
    string getName() const;

    /*
     * Getters and setters of the class Multimedia
     * @return path: path of the multimedia object
     */
    string getPath() const;

    /*
     * Getters and setters of the class Multimedia
     * @param name: name of the multimedia object
     */
    void setName(string name);

    /*
     * Getters and setters of the class Multimedia
     * @param path: path of the multimedia object
     */
    void setPath(string path);

    /*
     * Display the multimedia object with the name and the path
     * @param ostrm: output stream
     */
    virtual void display(std::ostream& ostrm) const;

    /*
     * Play the multimedia object
     */
    virtual void play() const = 0;
};

#endif // MULTIMEDIA_H
