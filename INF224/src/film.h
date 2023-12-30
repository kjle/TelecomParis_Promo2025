/*
 * @brief: subclass of Video, with additional parameters: chapiter
 */
#ifndef FILM_H
#define FILM_H

#include "video.h"

class Film : public Video
{
private:
    int chapiterNum;
    int *chapiters;
public:
/*
 * Constructor of the class Film with parameters
 * @param chapiterNum: number of chapiters
 * @param chapiters: array of chapiters
 */
    Film();
    Film(int chapiterNum, int *chapiters);
    Film(string name, string path, double length, int chapiterNum, int *chapiters);

/*
 * Destructor of the class Film
 */
    virtual ~Film();

/*
 * Getters and setters of the class Film
 * @param chapiterNum: number of chapiters
 * @param chapiters: array of chapiters
 */
    int getChapiterNum() const;
    int *getChapiters() const;
    void setChapiterNum(int chapiterNum);
    void setChapiters(int *chapiters);
    void setChapiterNumAndChapiters(int chapiterNum, int *chapiters);

/*
 * Display the film object with the name, the path, the length, the number of chapiters and the array of chapiters
 */
    void display(std::ostream& ostrm) const override;
};

#endif // FILM_H
