package org.seismotech.ground.lang;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public record CPUUsage(long total, long user) {

  public static CPUUsage none() {return new CPUUsage(0,0);}

  public static CPUUsage totalUser(long total, long user) {
    return new CPUUsage(total, user);
  }

  public static CPUUsage userSystem(long user, long sys) {
    return new CPUUsage(user+sys, user);
  }

  public long system() {return total-user;}

  public CPUUsage add(CPUUsage other) {
    return new CPUUsage(this.total + other.total, this.user + other.user);
  }

  public CPUUsage sub(CPUUsage other) {
    return new CPUUsage(this.total - other.total, this.user - other.user);
  }

  public CPUUsage delta(CPUUsage end) {return end.sub(this);}

  public static CPUUsage add(CPUUsage[] us) {
    long total = 0, user = 0;
    for (final CPUUsage u: us) {total += u.total;  user += u.user;}
    return totalUser(total, user);
  }

  //----------------------------------------------------------------------
  public static class Oracle {
    private final ThreadMXBean threads;

    public Oracle() {
      this.threads = ManagementFactory.getThreadMXBean();
    }

    private CPUUsage usage(long total, long user) {
      return total == -1 || user == -1 ? CPUUsage.none()
        : CPUUsage.totalUser(total, user);
    }

    public CPUUsage cpuCurrent() {
      final long user = threads.getCurrentThreadUserTime();
      final long total = threads.getCurrentThreadCpuTime();
      return usage(total, user);
    }

    public CPUUsage cpuOf(long id) {
      final long user = threads.getThreadUserTime(id);
      final long total = threads.getThreadCpuTime(id);
      return usage(total, user);
    }

    public CPUUsage[] cpuOf(long[] ids) {
      final CPUUsage[] us = new CPUUsage[ids.length];
      for (int i = 0; i < ids.length; i++) us[i] = cpuOf(ids[i]);
      return us;
    }

    public CPUUsage cpuAccum() {return cpuAccum(threads.getAllThreadIds());}

    public CPUUsage cpuAccum(long[] ids) {return add(cpuOf(ids));}
  }
}
