var ukmpApp = angular.module('ukmpApp', [ 'ui.bootstrap' ]);

ukmpApp.controller('UKMPCtrl', function($scope, $http) {
	
	$scope.setPage = function(pageNum) {
		$scope.refreshTweets(pageNum - 1);
	}
	
	$scope.search = function() {
		$http({ 
			method: 'GET',
			url: '/service/browse',
			params: { q: this.query }
		}).success(function(data) {
			$scope.updateModel(data);
		});
	}
	
	$scope.refreshTweets = function(page) {
		$http({
			method: 'GET',
			url: '/service/browse',
			params: { p: page }
		}).success(function(data) {
			$scope.updateModel(data);
		});
	}
	
	$scope.updateModel = function(data) {
		$scope.tweets = data.tweets;
		$scope.searchState = data.searchState;
		$scope.currentPage = $scope.searchState.pageNumber;
		$scope.numPages = data.numResults / data.pageSize;
		$scope.totalItems = data.numResults;
		
		rngStart = $scope.currentPage - 2;
		if (rngStart < 0) {
			rngStart = 0;
		}
		$scope.pgRange = [];
		for (i = rngStart; i < rngStart + 5; i ++) {
			$scope.pgRange.push(i);
		}
	}
	
});