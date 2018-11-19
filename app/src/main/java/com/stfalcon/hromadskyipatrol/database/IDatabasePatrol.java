/*
 * Copyright (c) 2015 - 2016. Stepan Tanasiychuk
 *
 *     This file is part of Gromadskyi Patrul is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Found ation, version 3 of the License, or any later version.
 *
 *     If you would like to use any part of this project for commercial purposes, please contact us
 *     for negotiating licensing terms and getting permission for commercial use.
 *     Our email address: info@stfalcon.com
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stfalcon.hromadskyipatrol.database;

import com.stfalcon.hromadskyipatrol.models.UserItem;
import com.stfalcon.hromadskyipatrol.models.VideoItem;

import java.util.ArrayList;

/**
 * Created by troy379 on 11.12.15.
 */
public interface IDatabasePatrol {

    void addVideo(VideoItem item);

    ArrayList<VideoItem> getVideos();
    ArrayList<VideoItem> getVideos(VideoItem.State state);
    VideoItem getVideo(VideoItem.State state);
    VideoItem getVideo(String id);

    ArrayList<VideoItem> getVideos(UserItem user);
    ArrayList<VideoItem> getVideos(VideoItem.State state, UserItem user);
    VideoItem getVideo(VideoItem.State state, UserItem user);
    VideoItem getVideo(String id, UserItem user);

    void updateVideo(String id, VideoItem.State state);
    void updateVideo(String id, String url);

    void deleteVideo(String id);
}
