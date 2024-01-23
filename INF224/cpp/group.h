#ifndef GROUP_H
#define GROUP_H

#include <iostream>
#include <list>
#include <memory>
#include "multimedia.h"

/// This file contains the declaration of the class Group,  which include a list of multimedia objects using shared pointers
class Group : public list< shared_ptr<Multimedia> >
{
private:
    string groupName;

public:

    Group();

/// @param groupName: name of the group
    Group(string groupName);

    virtual ~Group();

/// @return name of the group
    string getGroupName() const;

/// @param groupName: name of the group
    void setGroupName(const string groupName);

/// @brief insert a multimedia object to the list
/// @param multimedia: multimedia object to be inserted
    void insertObject (shared_ptr<Multimedia> multimedia);

/// @brief remove a multimedia object from the list
/// @param multimedia: multimedia object to be removed
    void removeObject (shared_ptr<Multimedia> multimedia);

/// @brief display the group object with the name and the list of multimedia objects
/// @param ostrm: output stream
    void display(std::ostream& ostr) const;
};

#endif // GROUP_H
