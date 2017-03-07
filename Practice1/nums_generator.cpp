//
//  nums_generator.cpp
//
//  Created by Gerry on 05/03/2017.
//  Copyright © 2017 Gao. All rights reserved.
//

#include <cstdlib>
#include <iostream>
#include <ctime>
#include <fstream>

using namespace std;

int main() {
    // File io
    fstream fs;
    fs.open("not_sorted.txt", fstream::out);
    
    // Prepare rand
    srand(time(0));
    
    // Get array length
    int n = 0;
    cout << "Please input the number of random numbers: \n";
    cin >> n;
    
    // Prompt user random value range
    cout << "Random value on [0 " << RAND_MAX << "].\n";
    
    int random_variable = 0;
    for (int i = 0; i < n; i++) {
        // Generate random number and append to file
        random_variable = rand();
        fs << random_variable << endl;
    }
    
    fs.close();
    
    cout << "Complete random numbers generation!\n";
    
    return 0;
}
