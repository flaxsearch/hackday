var ukmpApp = angular.module('ukmpApp', [ 'ui.bootstrap', 'ngSanitize' ]);

ukmpApp.controller('UKMPCtrl', function($scope, $http) {
	
	$scope.setPage = function(pageNum) {
		$scope.refreshTweets(pageNum - 1);
	}
	
	$scope.search = function() {
		$scope.updateModel({ q : this.query });
	}
	
	$scope.filter = function(field, value) {
		var params = {}
		if ($scope.searchState) {
			params.q = $scope.searchState.query;
			$scope.copyFilters(params);
			params.fq.push(field + ':"' + value + '"')
		}
		
		$scope.updateModel(params);
	}
	
	$scope.remove_filter = function(field, value) {
		var params = {}
		if ($scope.searchState) {
			params.q = $scope.searchState.query;
			$scope.copyFilters(params, field + ':"' + value + '"');
		}
		
		$scope.updateModel(params);
	}
	
	$scope.refreshTweets = function(page) {
		var params = {
			p: page
		};
		if ($scope.searchState) {
			params.q = $scope.searchState.query;
			$scope.copyFilters(params);
		}
		
		$scope.updateModel(params);
	}
	
	$scope.copyFilters = function(params, skip) {
		params.fq = [];
		var fields = Object.keys($scope.searchState.appliedFilters)
		if (fields.length > 0) {
			for (var i = 0; i < fields.length; i ++) {
				var field = fields[i];
				var filters = $scope.searchState.appliedFilters[field];
				for (var j = 0; j < filters.length; j ++) {
					var fq = field + ':"' + filters[j] + '"';
					if (fq !== skip) {
						params.fq.push(fq);
					}
				}
			}
		}
	}
	
	$scope.updateModel = function(params) {
		$http({
			method: 'GET',
			url: '/service/browse',
			params: params
		}).success(function(data) {
			// Update the basic scope data
			$scope.tweets = data.tweets;
			$scope.searchState = data.searchState;
			$scope.currentPage = $scope.searchState.pageNumber;
			$scope.numPages = data.numResults / data.pageSize;
			$scope.totalItems = data.numResults;
			
			// Set up a page range array - simplify the pagination component
			var rngStart = ($scope.currentPage - 2 < 0 ? 0 : $scope.currentPage - 2);
			$scope.pgRange = [];
			for (i = rngStart; i < rngStart + 5; i ++) {
				$scope.pgRange.push(i);
			}

			// Booleans indicating whether or not to display the searched/filtered by displays
			$scope.searched = false;
			if ($scope.searchState.query !== "*") {
				$scope.searched = true;
			}
			$scope.filtered = false;
			if (Object.keys($scope.searchState.appliedFilters).length > 0) {
				$scope.filtered = true;
			}
		});
	}
	
});