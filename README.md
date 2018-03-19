#App Store Service V2
A second version of the infamous app store service, meant for decoupling read and write ops. This is a completely stateless service that just serves API requests.

#Local dev environment setup

You need to install sbt 1.+: https://www.scala-sbt.org/

To seed cassandra data ASS-v2 uses the "dev" cassandra instance from https://github.com/apptentive/dev repo. Make sure you have it running.
After that, exectute "sbt seed" from the project root.

To run the service itself, execute "sbt run".

#API

#Get Apps by store

GET /api/v2/store/{store}/apps

store = {itunes, android}

Possible parameters:

page_size - max number of results to return

min_key - results are ordered by updatedDate, use this for requesting the next page by supplying the min value from the current page

as_of - essentailly same as min_key, but allows to pass date in user friendly yyyy-MM-dd format

Sample response:

    {
      "data": [
        {
          "store_id": "com.secretwhisper.bibleverses",
          "store": "Android",
          "category": null,
          "developer": "Secret Whispers",
          "title": "Messages from God",
          "icon": "https://lh3.ggpht.com/zcfcJhPoekGz3rioJIzWDXXOmFVnl4veP1Y_KrxwDdF7lm2OPxvo40N1t5e9xrGNjaab",
          "version": "tbd2",
          "updated_date": 1520293742000,
          "ingestion_time": 1520293742000
        }
      ],
      "page_size": 1,
      "has_more": true,
      "min_key": 1520293742000
    }

#Get app versions

GET /api/v2/store/{store}/apps/{storeId}/versions

Possible parameters:

page_size - max number of results to return

min_key - results are ordered by updatedDate, use this for requesting the next page by supplying the min value from the current page

as_of - essentailly same as min_key, but allows to pass date in user friendly yyyy-MM-dd format

Sample response:

    {
      "data": [
        {
          "store_id": "com.secretwhisper.bibleverses",
          "store": "Android",
          "category": null,
          "developer": "Secret Whispers",
          "title": "Messages from God",
          "icon": "https://lh3.ggpht.com/zcfcJhPoekGz3rioJIzWDXXOmFVnl4veP1Y_KrxwDdF7lm2OPxvo40N1t5e9xrGNjaab",
          "version": "tbd2",
          "updated_date": 1520293742000,
          "ingestion_time": 1520293742000
        }
      ],
      "page_size": 1,
      "has_more": true,
      "min_key": 1520293742000
    }

#Get a single app version

GET /api/v2/store/{store}/apps/{storeId}/versions/{version}

Possible parameters:

page_size - max number of results to return

min_key - results are ordered by updatedDate, use this for requesting the next page by supplying the min value from the current page

as_of - essentailly same as min_key, but allows to pass date in user friendly yyyy-MM-dd format

Sample response:

    {
      "store_id": "com.secretwhisper.bibleverses",
      "store": "Android",
      "category": null,
      "developer": "Secret Whispers",
      "title": "Messages from God",
      "icon": "https://lh3.ggpht.com/zcfcJhPoekGz3rioJIzWDXXOmFVnl4veP1Y_KrxwDdF7lm2OPxvo40N1t5e9xrGNjaab",
      "version": "tbd2",
      "updated_date": 1520293742000,
      "ingestion_time": 1520293742000
    }

#Get ratings of an app

GET /api/v2/store/{store}/apps/{storeId}/ratings-histograms

Possible parameters:

page_size - max number of results to return

min_key - results are ordered by ingestTime, use this for requesting the next page by supplying the min value from the current page

start_date - inclusive start date in yyyy-MM-dd format

end_date - inclusive end date in yyyy-MM-dd format

Sample response:

    {
      "data": [
        {
          "store_id": "1052194777",
          "store": "iOS",
          "region": "PL",
          "ingest_time": 1520439346000,
          "store_observed_time": 1520287200000,
          "all_ratings": {
            "5": 127,
            "1": 86,
            "2": 60,
            "3": 102,
            "4": 90
          }
        }
      ],
      "page_size": 1,
      "has_more": false,
      "min_key": 1520439346000
    }
    
#Get ratings of an app for a specific date

GET /api/v2/store/{store}/apps/{storeId}/ratings-histograms/{date}

Possible parameters:

page_size - max number of results to return

min_key - results are ordered by ingestTime, use this for requesting the next page by supplying the min value from the current page

date - date in yyyy-MM-dd format

Sample response:

    {
      "store_id": "1052194777",
      "store": "iOS",
      "region": "PL",
      "ingest_time": 1520439346000,
      "store_observed_time": 1520287200000,
      "all_ratings": {
        "5": 127,
        "1": 86,
        "2": 60,
        "3": 102,
        "4": 90
      }
    }