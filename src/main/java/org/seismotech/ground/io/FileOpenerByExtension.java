package org.seismotech.ground.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;

import org.seismotech.ground.util.Tuple2;

public class FileOpenerByExtension implements FileOpener {

  private final List<OpenDriver> drivers;

  public FileOpenerByExtension() {
    this.drivers = new ArrayList<>();
  }

  public FileOpenerByExtension loadDrivers(String criteria) {
    return loadDrivers(d -> d.name().contains(criteria));
  }

  public FileOpenerByExtension loadDrivers(Predicate<OpenDriver> criteria) {
    for (final OpenDriver d: ServiceLoader.load(OpenDriver.class)) {
      if (criteria.test(d)) drivers.add(d);
    }
    return this;
  }

  @Override
  public InputStream inputStream(Path path) throws IOException {
    final List<Tuple2<String,OpenDriver>> chain = new ArrayList<>(2);
    for (final String ext: Extensions.of(path)) {
      final OpenDriver d = driverFor(ext);
      if (d == null) break;
      chain.add(Tuple2.of(ext,d));
    }
    InputStream is = null;
    for (int i = chain.size() - 1; i >= 0; i--) {
      final Tuple2<String,OpenDriver> e = chain.get(i);
      final String ext = e._1();
      final OpenDriver d = e._2();
      is = (is == null) ? d.inputStream(ext, path) : d.inputStream(ext, is);
    }
    if (is == null) is = new FileInputStream(path.toFile());
    return is;
  }

  private OpenDriver driverFor(String ext) {
    for (final OpenDriver d: drivers) if (d.managesExtension(ext)) return d;
    return null;
  }
}
