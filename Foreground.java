public static class SortFileModified implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
        }
    }
    public static List<AndroidAppProcess> getRunningForegroundApps(Context context) { //need to call this method from Jenia's Code
        List<File> directoryListing=null;
        List<AndroidAppProcess> processes = new ArrayList<>();
        File[] files = new File("/proc").listFiles();
        if (files != null) {
             directoryListing= new ArrayList<File>();
            directoryListing.addAll(Arrays.asList(files));
            Collections.sort(directoryListing, new SortFileModified());
        }

        PackageManager pm = context.getPackageManager();
        for (File file : directoryListing) {
            if (file.isDirectory()) {
                int pid;
                try {
                    pid = Integer.parseInt(file.getName());
                } catch (NumberFormatException e) {
                    continue;
                }
                try {
                    AndroidAppProcess process = new AndroidAppProcess(pid);
                    if (process.foreground
                            // ignore system processes. First app user starts at 10000.
                            && (process.uid < 1000 || process.uid > 9999)
                            // ignore processes that are not running in the default app process.
                            && !process.name.contains(":")
                            // Ignore processes that the user cannot launch.
                            && pm.getLaunchIntentForPackage(process.getPackageName()) != null) {
                        processes.add(process);
                    }
                } catch (AndroidAppProcess.NotAndroidAppProcessException ignored) {
                } catch (IOException e) {
                    log(e, "Error reading from /proc/%d.", pid);
                    // System apps will not be readable on Android 5.0+ if SELinux is enforcing.
                    // You will need root access or an elevated SELinux context to read all files under /proc.
                }
            }
        }
        return processes;
    }
