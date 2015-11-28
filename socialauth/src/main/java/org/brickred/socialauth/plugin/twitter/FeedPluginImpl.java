/*
 ===========================================================================
 Copyright (c) 2012 3Pillar Global

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sub-license, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ===========================================================================

 */
package org.brickred.socialauth.plugin.twitter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brickred.socialauth.Feed;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.plugin.FeedPlugin;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.ProviderSupport;
import org.brickred.socialauth.util.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Feed Plugin implementation for Twitter
 *
 * @author tarun.nagpal
 */
public class FeedPluginImpl implements FeedPlugin, Serializable {

    private static final long serialVersionUID = 5091122799864049766L;
    private static final String FEED_URL = "https://api.twitter.com/1.1/statuses/home_timeline.json";
    private static final DateFormat dateFormat = new SimpleDateFormat(
            "EEE MMM dd hh:mm:ss z yyyy");
    private final Log LOG = LogFactory.getLog(this.getClass());

    private ProviderSupport providerSupport;

    public FeedPluginImpl(final ProviderSupport providerSupport) {
        this.providerSupport = providerSupport;
    }

    /**
     * Returns the list of feed. It returns maximum 20 feeds.
     *
     * @return List of feed
     * @throws Exception
     */
    @Override
    public List<Feed> getFeeds() throws Exception {
        Response response = null;
        List<Feed> list = new ArrayList<Feed>();
        LOG.info("Getting feeds from URL : " + FEED_URL);
        try {
            response = providerSupport.api(FEED_URL);
            String respStr = response
                    .getResponseBodyAsString(Constants.ENCODING);
            LOG.debug("Feeds json string :: " + respStr);
            JSONArray jarr = new JSONArray(respStr);
            LOG.debug("Feeds count :: " + jarr.length());
            for (int i = 0; i < jarr.length(); i++) {
                JSONObject jobj = jarr.getJSONObject(i);
                Feed feed = new Feed();
                String dateStr = jobj.optString("created_at", null);
                if (dateStr != null) {
                    feed.setCreatedAt(dateFormat.parse(dateStr));
                }
                feed.setMessage(jobj.optString("text", null));
                if (jobj.has("user")) {
                    JSONObject userObj = jobj.getJSONObject("user");
                    feed.setId(userObj.optString("id_str", null));
                    feed.setFrom(userObj.optString("name", null));
                    feed.setScreenName(userObj.optString("screen_name", null));
                }
                list.add(feed);
            }
        } catch (Exception e) {
            throw new SocialAuthException("Error while getting feeds from "
                    + FEED_URL, e);
        }
        return list;
    }

    @Override
    public ProviderSupport getProviderSupport() {
        return providerSupport;
    }

    @Override
    public void setProviderSupport(final ProviderSupport providerSupport) {
        this.providerSupport = providerSupport;

    }
}
