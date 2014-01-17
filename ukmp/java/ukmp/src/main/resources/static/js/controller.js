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
			if (facet.value.charAt(0) == '[') {
				// This is a facetQuery - don't quote the terms
				params.fq.push(facet.field + ":" + facet.value);
			} else {
				// This is a straight facet - quote the value
				params.fq.push(facet.field + ':"' + facet.value + '"')
			}
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
			// Incoming page number is 0-indexed
			$scope.currentPage = $scope.searchState.pageNumber + 1;
			$scope.numPages = data.numResults / data.pageSize;
			$scope.totalItems = data.numResults;
			
			// Booleans indicating whether or not to display the searched/filtered by displays
			$scope.searched = $scope.searchState.query !== "*";
			$scope.filtered = Object.keys($scope.searchState.appliedFilters).length > 0;
		});
	}
	
	this.addTweetTextLinks = function(tweets) {
		// Regex for matching a twitter username
		var usernameRx = /(@(\w+))/g;
		// Regex for matching a general link
		var linkRx = /(http:\/\/\S+)\b/g;
		// Loop through the text, adding links around each username found
		for (var i = 0; i < tweets.length; i ++) {
			var text = tweets[i].text;
			// Replace the links first, *then* the usernames
			var replaceText = text.replace(linkRx, '<a href="$1" target="_tweets">$1</a>')
			replaceText = replaceText.replace(usernameRx, '<a href="http://twitter.com/$2" target="_tweets">$1</a>');
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
})
.directive('facetAccordion', function() {
	return {
		restrict: 'E',
		scope: {
			facets: '=',
			isOpen: '=',
			click: '&'
		},
		templateUrl: 'template/directive/facet_accordion_group.html'
	}
})
.directive('facetQueryAccordion', function() {
	return {
		restrict: 'E',
		scope: {
			facets: '=',
			isOpen: '=',
			click: '&'
		},
		templateUrl: 'template/directive/facet_query_accordion_group.html'
	}
});