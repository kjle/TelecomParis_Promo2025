#include "group.h"

Group::Group() : list< shared_ptr<Multimedia> >(), groupName() {};

Group::Group(string groupName) : list< shared_ptr<Multimedia> >(), groupName(groupName) {};

Group::~Group() {
    cout << "Group Object: " << groupName << " deleted !" << endl;
}

string Group::getGroupName() const {
    return groupName;
}

void Group::setGroupName(const string groupName) {
    this->groupName = groupName;
}

void Group::insertObject(shared_ptr<Multimedia> multimedia) {
    this->push_back(multimedia);
}

void Group::removeObject(shared_ptr<Multimedia> multimedia) {
    this->remove(multimedia);
}

void Group::display(ostream& ostrm) const {
    ostrm << "Group Object: " << groupName << ": " << "-";
    for (auto & it : *this) {
        it->display(ostrm);
    }
    ostrm << "-";
}
