import ceylon.file {    parsePath,    Directory}shared void bug1712CR() {    value fileDir = parsePath("");    // look
    // comments!    assert(is Directory fileDir);}