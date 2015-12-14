/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stfalcon.hromadskyipatrol.utils;

import android.util.Log;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.googlecode.mp4parser.util.Matrix;
import com.googlecode.mp4parser.util.Path;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;
import java.util.List;


public class ProcessVideoUtils {
    private static final String TAG = ProcessVideoUtils.class.getName();

    /**
     * Shortens/Crops a track
     */
    public static boolean trimToLast20sec(File src, File dst) throws IOException {

        IsoFile isoFile = new IsoFile(src.getAbsolutePath());
        double duration = (double)
                isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
        Log.d(TAG, "trimToLast20sec: " + duration);
        if (duration > 20) {
            double startTime = duration - 20;
            startTrim(src, dst, startTime, duration);
            return true;
        }
        return false;
    }

    public static void startTrim(File src, File dst, double startTime, double endTime) throws IOException {
        FileDataSourceImpl file = new FileDataSourceImpl(src);
        Movie movie = MovieCreator.build(file);
        List<Track> tracks = movie.getTracks();
        movie.setTracks(new LinkedList<Track>());

        Log.d(TAG, "startTrim: " + startTime + " " + endTime);
        for (Track track : tracks) {
            long currentSample = 0;
            double currentTime = 0;
            long startSample = -1;
            long endSample = -1;
            for (int i = 0; i < track.getSampleDurations().length; i++) {
                if (currentTime <= startTime) {

                    // current sample is still before the new starttime
                    startSample = currentSample;
                }
                if (currentTime <= endTime) {
                    // current sample is after the new start time and still before the new endtime
                    endSample = currentSample;
                } else {
                    // current sample is after the end of the cropped video
                    break;
                }
                currentTime += (double) track.getSampleDurations()[i] / (double) track.getTrackMetaData().getTimescale();
                currentSample++;
            }
            movie.addTrack(new CroppedTrack(track, startSample, endSample));
        }
        Container out = new DefaultMp4Builder().build(movie);
        MovieHeaderBox mvhd = Path.getPath(out, "moov/mvhd");
        mvhd.setMatrix(Matrix.ROTATE_180);
        if (!dst.exists()) {
            dst.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(dst);
        WritableByteChannel fc = fos.getChannel();
        try {
            out.writeContainer(fc);
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            fc.close();
            fos.close();
            file.close();
        }
    }


    public static boolean concatTwoVideos(File src1, File src2, File dst) {
        try {
            FileDataSourceImpl file1 = new FileDataSourceImpl(src1);
            FileDataSourceImpl file2 = new FileDataSourceImpl(src2);
            Movie result = new Movie();
            Movie movie1 = MovieCreator.build(file1);
            Movie movie2 = MovieCreator.build(file2);

            Movie[] inMovies = new Movie[]{
                    movie1, movie2
            };

            List<Track> videoTracks = new LinkedList<Track>();
            List<Track> audioTracks = new LinkedList<Track>();

            for (Movie m : inMovies) {
                for (Track t : m.getTracks()) {
                    if (t.getHandler().equals("soun")) {
                        audioTracks.add(t);
                    }
                    if (t.getHandler().equals("vide")) {
                        videoTracks.add(t);
                    }
                }
            }

            if (audioTracks.size() > 0) {

                result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));

            }
            if (videoTracks.size() > 0) {

                result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));

            }

            Container out = new DefaultMp4Builder().build(result);
            MovieHeaderBox mvhd = Path.getPath(out, "moov/mvhd");
            mvhd.setMatrix(Matrix.ROTATE_180);
            if (!dst.exists()) {
                dst.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(dst);
            WritableByteChannel fc = fos.getChannel();
            try {
                out.writeContainer(fc);
            } finally {
                fc.close();
                fos.close();
                file1.close();
                file2.close();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}