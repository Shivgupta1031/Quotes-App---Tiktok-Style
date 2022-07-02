package bestquotes.aboutlive.com

class Variables {

    companion object {

        @JvmStatic
        val TAG: String = "MyTag"

        @JvmStatic
        val API_KEY: String = "key3WQzZcQI8dAXD7"

        @JvmStatic
        val BASE_KEY: String = "appjO9FlcXEjm0UV0"

        @JvmStatic
        val AIRTABLE_QUOTES_TABLE_URL: String =
            "https://api.airtable.com/v0/$BASE_KEY/Quotes Table?view=Grid%20view&api_key=$API_KEY&offset="

        @JvmStatic
        val AIRTABLE_SETTINGS_TABLE_URL: String =
            "https://api.airtable.com/v0/$BASE_KEY/Settings?view=Grid%20view&api_key=$API_KEY"

        @JvmStatic
        var SHOW_ADS: Boolean = true

        @JvmStatic
        var SHOW_ADMOB_ADS: Boolean = true

        @JvmStatic
        var ADMOB_APP_ID: String = "ca-app-pub-3940256099942544~3347511713"

        @JvmStatic
        var BANNER_ID: String = "Quotes Table"

        @JvmStatic
        var INTERSTITIAL_ID: String = "Quotes Table"

        @JvmStatic
        var NATIVE_ID: String = "Quotes Table"

        @JvmStatic
        var ONESIGNAL_APP_ID: String = "########-####-####-####-############"

        @JvmStatic
        var SHOW_NATIVE_ADS_AFTER_QUOTE: Int = 4

        @JvmStatic
        var SHOW_INTERSTITIAL_ADS_AFTER_QUOTE: Int = 4

        @JvmStatic
        var PRIVACY_POLICY: String = ""
    }

}