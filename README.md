# MoPub Android SDK

Thanks for taking a look at MoPub! We take pride in having an easy-to-use, flexible monetization solution that works across multiple platforms.

Sign up for an account at [http://app.mopub.com/](http://app.mopub.com/).

## Need Help?

You can find integration documentation on our [wiki](https://github.com/mopub/mopub-android-sdk/wiki/Getting-Started) and
additional help documentation on our [developer help site](http://dev.twitter.com/mopub). 

To file an issue with our team visit the [MoPub Forum](https://twittercommunity.com/c/fabric/mopub) or email [support@mopub.com](mailto:support@mopub.com).

**Please Note: We no longer accept GitHub Issues**

## Download

The MoPub SDK is distributed as source code that you can include in your application.  MoPub provides two prepackaged archives of source code:

- **[MoPub Android Full SDK.zip](http://bit.ly/YUdU9v)**

  Includes everything you need to serve HTML and MRAID MoPub advertisiments *and* built-in support for Millennial Media third party ad network - [Millennial Media](http://www.millennialmedia.com/) - including the required third party binaries.

- **[MoPub Android Base SDK.zip](http://bit.ly/YUdWhH)**

  Includes everything you need to serve HTML and MRAID MoPub advertisements.  No third party ad networks are included.

## Integrate

Integration instructions are available on the [wiki](https://github.com/mopub/mopub-android-sdk/wiki/Getting-Started).


## New in this Version

Please view the [changelog](https://github.com/mopub/mopub-android-sdk/blob/master/CHANGELOG.md) for details.

- Added **VAST 3.0** standard support for video ads.
- Improved **video player UX**.
- Added **RecyclerView** support for native ads. See the [integration guide](https://github.com/mopub/mopub-android-sdk/wiki/Native-Ads-with-Recycler-View).
- Improved **deep link** handling.
- Bug Fixes:
  - MRAID video interstitials now play automatically when displayed on Jellybean MR1 and newer.
  - MRAID relative assets are correctly rendered.
  - MoPubLog no longer duplicates some messages.

## Requirements

- Android 2.3.1 (API Version 9) and up
- android-support-v4.jar, r22 (**Updated in 3.7.0**)
- android-support-annotations.jar, r22 (**Updated in 3.7.0**)
- android-support-v7-recyclerview.jar, r22 (**Updated in 3.9.0**)
- MoPub Volley Library (mopub-volley-1.1.0.jar - available on JCenter) (**Updated in 3.6.0**)
- **Recommended** Google Play Services 7.0.0

## Upgrading from 3.2.0 and Prior
In 3.3.0 a dependency on android-support-annotations.jar was added. If you are using Maven or Gradle to include the MoPub SDK, this dependency is included in the build scripts. For instructions on adding dependencies for Eclipse projects, see our [Getting Started Guide](https://github.com/mopub/mopub-android-sdk/wiki/Getting-Started#adding-the-support-libraries-to-your-project)

## License

We have launched a new license as of version 3.2.0. To view the full license, visit [http://www.mopub.com/legal/sdk-license-agreement/](http://www.mopub.com/legal/sdk-license-agreement/).
