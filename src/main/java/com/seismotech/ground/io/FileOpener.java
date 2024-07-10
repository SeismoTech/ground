package com.seismotech.ground.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface FileOpener {

  InputStream inputStream(Path path) throws IOException;
}
