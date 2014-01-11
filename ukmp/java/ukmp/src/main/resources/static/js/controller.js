var ukmpApp = angular.module('ukmpApp', [ 'ui.bootstrap', 'ngSanitize' ]);

ukmpApp.controller('UKMPCtrl', [ '$scope', '$http', function($scope, $http) {
	
	var self = this;
	
	$scope.setPage = function(page) {
		var params = {
				p: page - 1
		};
		if ($scope.searchState) {
			params.q = $scope.searchState.query;
			self.copyFilters(params);
		}

		self.updateModel(params);
	}
	
	$scope.search = function() {
		self.updateModel({ q : this.query });
	}
	
	$scope.filter = function(facet) {
		var params = {}
		if ($scope.searchState) {
			params.q = $scope.searchState.query;
			self.copyFilters(params);
			params.fq.push(facet.field + ':"' + facet.value + '"')
		}
		
		self.updateModel(params);
	}
	
	$scope.remove_filter = function(field, value) {
		var params = {}
		if ($scope.searchState) {
			params.q = $scope.searchState.query;
			self.copyFilters(params, field + ':"' + value + '"');
		}
		
		self.updateModel(params);
	}
	
	this.copyFilters = function(params, skip) {
		params.fq = [];
		var fields = Object.keys($scope.searchState.appliedFilters)
		if (fields.length > 0) {
			for (var i = 0; i < fields.length; i ++) {
				var field = fields[i];
				var filters = $scope.searchState.appliedFilters[field].facets;
				for (var j = 0; j < filters.length; j ++) {
					var fq = field + ':"' + filters[j].value + '"';
					if (fq !== skip) {
						params.fq.push(fq);
					}
				}
			}
		}
	}
	
	this.updateModel = function(params) {
		$http({
			method: 'GET',
			url: '/service/browse',
			params: params
		}).success(function(data) {
			// Update the basic scope data
			$scope.tweets = data.tweets;
			self.addTweetTextLinks($scope.tweets);
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
			$scope.searched = $scope.searchState.query !== "*";
			$scope.filtered = Object.keys($scope.searchState.appliedFilters).length > 0;
		});
	}
	
	this.addTweetTextLinks = function(tweets) {
		// Regex for matching a twitter username
		var usernameRx = /(@(\w+))/g;
		// Loop through the text, adding links around each username found
		for (var i = 0; i < tweets.length; i ++) {
			var text = tweets[i].text;
			var replaceText = text.replace(usernameRx, '<a href="http://twitter.com/$2" target="_tweets">$1</a>');
			tweets[i].text = replaceText;
		}
	}
	
	this.init = function() {
		$scope.setPage(1);
	}
	
	self.init();
	
}]);

ukmpApp.directive('facetList', function() {
	return {
		restrict: 'E',
		scope: {
			facets: '=facets',
			click: '&facetClick'
		},
		templateUrl: 'template/directive/facet_list.html'
	};
});