prma (Prediction machine)
====

```
Endpoints:

/version/api
This will be endpoint of the restful interface.Different versions of the api will have different endpoints. Version x, subversion y will have endpoint vx.y/api/

/api/training
/api/prediction
/api/extractor


Creating a new model
/api/training
@POST accepts(ModelTrainingRequest) responds with (ModelTrainingResponse)

Getting status on training
/api/training/{modelname}
@GET returns (TrainingStatusResponse)

Getting meta-data on a model
/api/training/{modelname}
@HEAD returns (ModelStatus)

Updating an existing model
/api/training/{modelname}
@PUT accepts(ModelTrainingRequest) responds with (ModelTrainingResponse)

Getting prediction from a model
/api/prediction/{modelname}
@POST accepts(PredictionRequest) responds with (PredictionResponse)
@POST accepts(BatchPredictionRequest) responds with (BatchPredictionResponse)


Not sure what to do about these endpoints
/api/algorithms/classifier
/api/algorithms/regression
/api/algorithms/recommendation
/api/algorithms/neighbors
/api/algorithms/perception/
/api/algorithms/perception/vision
/api/algorithms/perception/speech

```
