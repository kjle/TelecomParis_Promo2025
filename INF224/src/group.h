/*
 * @brief: This file contains the declaration of the class Group,  which include a list of multimedia objects using shared pointers
 */
#ifndef GROUP_H
#define GROUP_H

#include <iostream>
#include <list>
#include <memory>
#include "multimedia.h"

class Group : public list< shared_ptr<Multimedia> >
{
private:
    string groupName;

public:
/*
 * Constructor of the class Group with parameters
  * @param groupName: name of the group
 */
    Group();
    Group(string groupName);

/*
 * Destructor of the class Group
 */
    virtual ~Group();

/*
 * Getters and setters of the class Group
 * @return groupName: name of the group
 * @param groupName: name of the group
 */
    string getGroupName() const;
    void setGroupName(const string groupName);

/*
 * Insert and remove multimedia objects in the list
 * @param multimedia: pointer of multimedia object to insert or remove
 */
    void insertObject (shared_ptr<Multimedia> multimedia);
    void removeObject (shared_ptr<Multimedia> multimedia);

/*
 * Display the group object with the name and the list of multimedia objects
 */
    void display(std::ostream& ostr) const;
};

#endif // GROUP_H
