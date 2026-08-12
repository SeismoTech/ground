package org.seismotech.ground.io.watch;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.seismotech.ground.io.XFile;

class TraversalWatcherTest {

  @Test
  void testMachine() throws IOException {
    final Path root = Files.createTempDirectory("ground-watcher.");
    try {
      final var events = new HashMap<Path,WatchEvent>();
      final var twm = new TraversalWatcherMachine(
        List.of(root),
        p -> p.getFileName().toString().endsWith(".log"),
        e -> events.put(e.attr().name(), e));
      twm.traverse();
      assertTrue(events.isEmpty());

      final Path file1 = Files.createTempFile(root, "file1.", ".log");
      final Path file2 = Files.createTempFile(root, "file2.", ".log");
      final Path file3 = Files.createTempFile(root, "file3.", ".txt");
      twm.traverse();
      assertEquals(2, events.size());

      final Path nested1 = Files.createTempDirectory(root, "nested1.");
      final Path file11 = Files.createTempFile(nested1, "file11.", ".log");
      final Path file12 = Files.createTempFile(nested1, "file12.", ".log");
      final Path file13 = Files.createTempFile(nested1, "file13.", ".txt");
      events.clear();
      twm.traverse();
      assertEquals(2, events.size());

      Files.write(file1, new byte[16]);
      Files.write(file12, new byte[16]);
      events.clear();
      twm.traverse();
      assertEquals(2, events.size());

      Files.delete(file2);
      Files.delete(file11);
      events.clear();
      twm.traverse();
      assertEquals(2, events.size());
      System.err.println(events);

      Files.delete(file3);
      Files.delete(file13);
      events.clear();
      twm.traverse();
      assertEquals(0, events.size());
    } finally {
      //XFile.deleteTree(root);
    }
  }
}
