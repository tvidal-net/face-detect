#!/usr/bin/env bash -e

SOURCE_DIR='/tmp/opencv-3.2.0'
TARGET_DIR='/var/lib/opencv'

if [ ! -d ${SOURCE_DIR} ]; then
    echo "Downloading opencv-3.2.0 sources..."
    curl -LSskf 'https://github.com/opencv/opencv/archive/3.2.0.tar.gz' | tar zxv -C $(dirname ${SOURCE_DIR})
    echo
fi

echo "Generating Makefiles"
sudo install -o ${USER} -g admin -m 0755 -d ${TARGET_DIR}
cd ${TARGET_DIR}
cmake -G "Unix Makefiles" ${SOURCE_DIR}
echo

echo "Building opencv-3.2.0"
make -j8
sudo make install
echo

echo "Done."
