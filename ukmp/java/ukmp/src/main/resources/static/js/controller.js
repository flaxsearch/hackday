var ukmpApp = angular.module('ukmpApp', [ 'ui.bootstrap' ]);

ukmpApp.controller('UKMPCtrl', function($scope, $http) {
	
	$scope.setPage = function(pageNum) {
		$scope.refreshTweets(pageNum - 1);
	}
	
	$scope.refreshTweets = function(page) {
		$http.get('/service/browse?p=' + page).success(function(data) {
			$scope.tweets = data.tweets;
			$scope.currentPage = data.start / data.pageSize;
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
		});
	}
	
});