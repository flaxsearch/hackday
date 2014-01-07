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
	
	$scope.filter = function(field, value) {
		var params = {}
		if ($scope.searchState) {
			params.q = $scope.searchState.query;
			$scope.copyFilters(params);
			params.fq.push(field + ":" + value)
		}
		
		$http({
			method: 'GET',
			url: '/service/browse',
			params: params
		}).success(function(data) {
			$scope.updateModel(data);
		});
	}
	
	$scope.remove_filter = function(field, value) {
		var params = {}
		if ($scope.searchState) {
			params.q = $scope.searchState.query;
			$scope.copyFilters(params, field + ":" + value);
		}
		
		$http({
			method: 'GET',
			url: '/service/browse',
			params: params
		}).success(function(data) {
			$scope.updateModel(data);
		});
	}
	
	$scope.refreshTweets = function(page) {
		var params = {
			p: page
		};
		if ($scope.searchState) {
			params.q = $scope.searchState.query;
			$scope.copyFilters(params);
		}
		
		$http({
			method: 'GET',
			url: '/service/browse',
			params: params
		}).success(function(data) {
			$scope.updateModel(data);
		});
	}
	
	$scope.copyFilters = function(params, skip) {
		params.fq = [];
		var fields = Object.keys($scope.searchState.appliedFilters)
		if (fields.length > 0) {
			for (var i = 0; i < fields.length; i ++) {
				var field = fields[i];
				var filters = $scope.searchState.appliedFilters[field];
				for (var j = 0; j < filters.length; j ++) {
					var fq = field + ":" + filters[j];
					if (fq !== skip) {
						params.fq.push(field + ":" + filters[j]);
					}
				}
			}
		}
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
		
		$scope.searched = false;
		if ($scope.searchState.query !== "*") {
			$scope.searched = true;
		}
		$scope.filtered = false;
		if (Object.keys($scope.searchState.appliedFilters).length > 0) {
			$scope.filtered = true;
		}
		$scope.displaySearch = $scope.searched || $scope.filtered;
	}
	
});