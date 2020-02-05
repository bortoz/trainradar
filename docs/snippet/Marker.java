Marker marker = googleMap.addMarker(new MarkerOptions()
    .position(pos)
    .title(train.getName())
    .icon(BitmapDescriptorFactory.fromBitmap(TrainManager.getTrainIcon())));
marker.setTag(train);
