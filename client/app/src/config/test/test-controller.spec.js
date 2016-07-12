'use strict';

describe('TestCtrl', function () {
    
    beforeEach(module('SgmapRetraiteConfig'));
    
    var $scope, controller, CheckList, ApiRegimes, ApiUserChecklist;
    
    beforeEach(inject(function ($rootScope, $controller, _CheckList_, _ApiRegimes_, _ApiUserChecklist_) {

        CheckList = _CheckList_;
        ApiRegimes = _ApiRegimes_;
        ApiUserChecklist = _ApiUserChecklist_;
        
    }));
    
    var mockRegimes = ["CAVIMAC","CNAV","MSA","RSI"];

    beforeEach(function() {
        spyOn(ApiRegimes, 'getRegimes').and.returnValue({
            then: function(onSuccess, onError) {
                onSuccess(mockRegimes);
            }
        });
    });

    beforeEach(inject(function ($rootScope, $controller, _CheckList_) {

        $scope = $rootScope.$new();
        controller = $controller('TestCtrl', {
            $scope: $scope            
        });

    }));
    
    it('should have init data in scope', function () {
        expect($scope.regimes).toEqual(mockRegimes);
        
        expect($scope.departements.length).toEqual(101);
        expect($scope.departements[0]).toEqual("01");
        expect($scope.departements[19]).toEqual("2A");
        expect($scope.departements[20]).toEqual("2B");
        expect($scope.departements[21]).toEqual("21");
        expect($scope.departements[96]).toEqual("971");
        expect($scope.departements[99]).toEqual("974");
        expect($scope.departements[100]).toEqual("976");

        expect($scope.listeMoisAvecPremier).toBeDefined();
        expect($scope.listeAnneesDepart).toBeDefined();
        expect($scope.multiselectRegimesSettings).toBeDefined();
        expect($scope.multiselectRegimesTexts).toBeDefined();
        
        expect($scope.data.nom).toEqual("DUPONT");
        expect($scope.data.departMois).toEqual(1);
        expect($scope.data.departAnnee).toEqual(2020);
        expect($scope.data.regimeLiquidateur).toEqual('CNAV');
        expect($scope.data.regimes).toEqual([]);
    });

    describe('should get checklist and store result in scope', function () {
        
        beforeEach(function() {
            $scope.data = {
                nom: "DUPONT",
                dateNaissance: "07/10/1954",
                nir: "1541014123456"
            };

            spyOn(ApiUserChecklist, 'getChecklistUrl').and.returnValue("bla bla bla");
            spyOn($scope, 'reloadIFrame');
        });
        
        it('should get URL for published=true', function () {
            
            $scope.test(true);

            expect($scope.testUrlForIFrame).toEqual('bla bla bla');
            expect(ApiUserChecklist.getChecklistUrl).toHaveBeenCalledWith($scope.data, true, true);
            expect($scope.reloadIFrame).toHaveBeenCalled();
        });
        
        it('should get URL for published=false', function () {
            
            $scope.test(false);

            expect($scope.testUrlForIFrame).toEqual('bla bla bla');
            expect(ApiUserChecklist.getChecklistUrl).toHaveBeenCalledWith($scope.data, false, true);
            expect($scope.reloadIFrame).toHaveBeenCalled();
        });
    });

});

